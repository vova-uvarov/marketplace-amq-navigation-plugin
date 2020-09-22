package com.vuvarov.marketplace;

import com.intellij.ide.hierarchy.call.JavaCallHierarchyProvider;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class Front2BackCallHierarchyProvider extends JavaCallHierarchyProvider {
    @Override
    public PsiElement getTarget(@NotNull DataContext dataContext) {
        return super.getTarget(dataContext);
    }

}
