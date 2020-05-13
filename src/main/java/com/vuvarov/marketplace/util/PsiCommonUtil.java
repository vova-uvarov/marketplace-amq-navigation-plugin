package com.vuvarov.marketplace.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiReturnStatement;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.tree.java.PsiMethodCallExpressionImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.siyeh.ig.psiutils.MethodUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PsiCommonUtil {

    public static PsiExpression getReturnValue(PsiMethod method) {
        PsiReturnStatement returnStatement = getReturnStatement(method);
        if (returnStatement != null) {
            return returnStatement.getReturnValue();
        }
        return null;
    }

    public static PsiReturnStatement getReturnStatement(PsiMethod method) {
        PsiCodeBlock body = method.getBody();
        if (body == null) {
            return null;
        }
        PsiStatement[] statements = body.getStatements();
        return (PsiReturnStatement) Arrays.stream(statements)
                .filter(s -> s instanceof PsiReturnStatement)
                .findAny()
                .orElse(null);
    }

    public static List<PsiMethod> getAllSuperMethods(PsiMethod method) {
        List<PsiMethod> result = new ArrayList<>();
        PsiMethod searchedMethod = method;
        while (searchedMethod != null) {
            result.add(searchedMethod);
            searchedMethod = MethodUtils.getSuper(searchedMethod);
        }
        return result;
    }

    public static List<PsiMethod> findMethods(GlobalSearchScope searchScope, PsiClass psiClass, List<String> methodNames) {
        if (searchScope == null) {
            return Collections.emptyList();
        }

        return methodNames.stream()
                .map(methodName -> psiClass.findMethodsByName(methodName, false))
                .flatMap(Stream::of)
                .collect(Collectors.toList());
    }

    public static PsiClass getClassByName(GlobalSearchScope searchScope, String className) {
        Project project = searchScope.getProject();
        if (project != null) {
            JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(project);
            return psiFacade.findClass(className, searchScope);
        }
        return null;
    }

    public static Module getModule(PsiElement element) {
        return ModuleUtilCore.findModuleForPsiElement(element);
    }

    public static GlobalSearchScope getScope(PsiElement element) {
        Module module = ModuleUtilCore.findModuleForPsiElement(element);
        if (module != null) {
            return module.getModuleScope(false);
        }
        return null;
    }


    public static String getCallMethodName(@NotNull PsiMethodCallExpressionImpl element) {
        return element.getMethodExpression().getReferenceName();
    }

    public static PsiClass getQuelifierClass(PsiMethodCallExpression methodCallExpression) {
        PsiMethod psiMethod = methodCallExpression.resolveMethod();
        if (psiMethod == null) return null;
        return psiMethod.getContainingClass();
    }

    public static PsiElement getCallableMethodNameElement(PsiMethodCallExpression element) {
        PsiElement[] children = element.getMethodExpression().getChildren();
        return Arrays.stream(children)
                .filter(e -> e instanceof PsiIdentifier)
                .findAny()
                .orElse(null);
    }

}
