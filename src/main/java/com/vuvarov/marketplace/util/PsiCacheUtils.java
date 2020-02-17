package com.vuvarov.marketplace.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;

import java.util.Collection;
import java.util.function.Supplier;

public class PsiCacheUtils {
    public static Collection<? extends PsiElement> getCachedElements(PsiElement element, Supplier<Collection<? extends PsiElement>> valueSupplier) {
        Project project = PsiCommonUtil.getModule(element).getProject();
        return CachedValuesManager.getCachedValue(element, () -> CachedValueProvider.Result.create(valueSupplier.get(),
                PsiModificationTracker.MODIFICATION_COUNT, //track  code changes everywhere
                ProjectRootManager.getInstance(project) // and in project structure
        ));
    }
}
