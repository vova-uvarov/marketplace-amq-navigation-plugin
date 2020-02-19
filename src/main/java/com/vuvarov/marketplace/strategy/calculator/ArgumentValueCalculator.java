package com.vuvarov.marketplace.strategy.calculator;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiEnumConstant;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.search.searches.OverridingMethodsSearch;
import com.vuvarov.marketplace.util.PsiCommonUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
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
                return valueFromExpression(((PsiVariable) resolvedElement).getInitializer());
            }
        }
        return Collections.emptySet();
    }
}
