package com.vuvarov.marketplace.strategy.target.searcher;

import com.intellij.codeInsight.dataflow.SetUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.util.Query;
import com.vuvarov.marketplace.strategy.calculator.ArgumentValueCalculator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class TargetSearcher {

    public static final int MQENTITY_ARGUMENT_INDEX = 0;
    public static final int MQOPERATION_ARGUMENT_INDEX = 1;

    private static final ArgumentValueCalculator calculator = new ArgumentValueCalculator();

    protected abstract List<PsiMethod> getPotencialMethods(PsiElement element);

    public Collection<? extends PsiElement> searchTargets(@NotNull PsiElement element) {
        return searchTargetsInner(element, getPotencialMethods(element));
    }

    @NotNull
    private Collection<? extends PsiElement> searchTargetsInner(@NotNull PsiElement
                                                                        element, List<PsiMethod> potencialMethods) {
        List<PsiElement> resultElements = new ArrayList<>();
        Set<String> sourceMqEntity = calculator.argumentValues((PsiMethodCallExpression) element, MQENTITY_ARGUMENT_INDEX);
        Set<String> sourceMqOperation = calculator.argumentValues((PsiMethodCallExpression) element, MQOPERATION_ARGUMENT_INDEX);

        for (PsiMethod method : potencialMethods) {
            Query<PsiReference> search = MethodReferencesSearch.search(method);
            List<PsiElement> collect = search.findAll().stream()
                    .filter(searchResult -> isTarget(sourceMqEntity, sourceMqOperation, searchResult))
                    .map(PsiReference::getElement)
                    .collect(Collectors.toList());
            resultElements.addAll(collect);
        }
        return resultElements;
    }

    private boolean isTarget(Set<String> sendMqEntity, Set<String> sendMqOperation, PsiReference searchResult) {
        PsiElement searchResultElement = searchResult.getElement().getParent();
        Set<String> mqEntity = calculator.argumentValues((PsiMethodCallExpression) searchResultElement, MQENTITY_ARGUMENT_INDEX);
        Set<String> mqOperation = calculator.argumentValues((PsiMethodCallExpression) searchResultElement, MQOPERATION_ARGUMENT_INDEX);
        return !SetUtil.intersect(mqEntity, sendMqEntity).isEmpty() && !SetUtil.intersect(mqOperation, sendMqOperation).isEmpty();
    }
}
