package com.vuvarov.marketplace.strategy.creator;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.vuvarov.marketplace.util.PsiCommonUtil.getMethodNameElement;

public interface MarkerInfoCreator {

    default RelatedItemLineMarkerInfo<PsiElement> createLineMarkerInfo(PsiMethodCallExpression element) {
        NavigationGutterIconBuilder<PsiElement> gutterIcon = NavigationGutterIconBuilder.create(getIcon())
                .setTargets(NotNullLazyValue.createValue(() -> searchTargets(element)))
                .setTooltipText(getTooltipText());
        return gutterIcon.createLineMarkerInfo(getMethodNameElement(element));
    }

    String getTooltipText();

    Icon getIcon();

    Collection<? extends PsiElement> searchTargets(PsiElement element);
}
