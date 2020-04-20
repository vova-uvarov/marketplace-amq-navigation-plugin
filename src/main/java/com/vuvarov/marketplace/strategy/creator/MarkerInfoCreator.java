package com.vuvarov.marketplace.strategy.creator;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethodCallExpression;

import javax.swing.*;
import java.util.Collection;

import static com.vuvarov.marketplace.util.PsiCommonUtil.getCallableMethodNameElement;

public interface MarkerInfoCreator {

    default RelatedItemLineMarkerInfo<PsiElement> createLineMarkerInfo(PsiMethodCallExpression element) {
        NavigationGutterIconBuilder<PsiElement> gutterIcon = NavigationGutterIconBuilder.create(getIcon())
                .setTargets(NotNullLazyValue.createValue(() -> searchTargets(element)))
                .setCellRenderer(new ElementListCellRenderer())
                .setEmptyPopupText("Target not found")
                .setTooltipText(getTooltipText());
        return gutterIcon.createLineMarkerInfo(getCallableMethodNameElement(element));
    }

    String getTooltipText();

    Icon getIcon();

    Collection<? extends PsiElement> searchTargets(PsiElement element);
}
