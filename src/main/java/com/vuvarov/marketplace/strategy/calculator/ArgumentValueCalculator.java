package com.vuvarov.marketplace.strategy.calculator;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.psi.search.searches.OverridingMethodsSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Query;
import com.siyeh.ig.psiutils.MethodUtils;
import com.vuvarov.marketplace.util.PsiCommonUtil;

import java.util.*;
import java.util.stream.Collectors;

import static com.vuvarov.marketplace.util.PsiCommonUtil.getReturnValue;

public class ArgumentValueCalculator {
    public Set<String> argumentValues(PsiMethodCallExpression methodCallExpression, int index) {
        PsiExpression argumentExpression = methodCallExpression.getArgumentList().getExpressions()[index];
        if (argumentExpression instanceof PsiMethodCallExpression) {
            PsiMethod psiMethod = ((PsiMethodCallExpression) argumentExpression).resolveMethod();
            if (psiMethod != null) {
                if (psiMethod.hasModifierProperty(PsiModifier.ABSTRACT)) {
                    Collection<PsiMethod> overridinMethods = OverridingMethodsSearch.search(psiMethod).findAll();
                    return overridinMethods.stream()
                            .map(m -> valueFromExpression(getReturnValue(m))).filter(Objects::nonNull)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toSet());

                } else {
                    PsiExpression returnValueExpression = PsiCommonUtil.getReturnValue(psiMethod);
                    return valueFromExpression(returnValueExpression);
                }
            }
        }

        return valueFromExpression(argumentExpression);
    }

    private Set<String> valueFromExpression(PsiExpression expression) {
        if (expression == null) return Collections.emptySet();
        PsiReference reference = expression.getReference();
        if (reference != null) {
            PsiElement resolvedElement = reference.resolve();
            if (resolvedElement instanceof PsiEnumConstant) {
                return Collections.singleton(((PsiEnumConstant) resolvedElement).getName());
            }

            if (resolvedElement instanceof PsiVariable) {
                if (resolvedElement instanceof PsiParameter) {
                    PsiMethod method = PsiTreeUtil.getParentOfType(expression.getOriginalElement(), PsiMethod.class);
                    if (method != null) {
                        List<PsiMethod> allMethods = PsiCommonUtil.getAllSuperMethods(method);
                        return allMethods.stream()
                                .map(MethodReferencesSearch::search)
                                .flatMap(q -> q.findAll().stream())
                                .map(r -> {
                                            if (r instanceof PsiMethodCallExpression) {
                                                return (PsiMethodCallExpression) r;
                                            }
                                            return (PsiMethodCallExpression)((PsiReferenceExpression) r).getParent();
                                        }
                                )
                                .map(m -> argumentValues(m, method.getParameterList().getParameterIndex((PsiParameter) resolvedElement)))
                                .flatMap(Collection::stream)
                                .collect(Collectors.toSet());
                    }
                }
                return valueFromExpression(((PsiVariable) resolvedElement).getInitializer());
            }
        }
        return Collections.emptySet();
    }
}
