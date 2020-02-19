package com.vuvarov.marketplace.strategy.creator;

import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.Nullable;

public class ElementListCellRenderer extends PsiElementListCellRenderer<PsiElement> {
    @Override
    public String getElementText(PsiElement element) {
        PsiClass topLevelClass = PsiUtil.getTopLevelClass(element);
        if (topLevelClass == null) return "";
        PsiIdentifier nameIdentifier = topLevelClass.getNameIdentifier();
        if (nameIdentifier == null) return "";
        return nameIdentifier.getText();
    }

    @Nullable
    @Override
    protected String getContainerText(PsiElement element, String name) {
        PsiMethod parent = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        if (parent == null) return "";
        return parent.getName();
    }

    @Override
    protected int getIconFlags() {
        return 0;
    }
}
