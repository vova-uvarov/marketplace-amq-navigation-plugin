package com.vuvarov.marketplace.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiReturnStatement;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.tree.java.PsiMethodCallExpressionImpl;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

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

    public static List<PsiMethod> findMethods(GlobalSearchScope searchScope, String className, List<String> methodNames) {
        if (searchScope == null) {
            return Collections.emptyList();
        }
        Project project = searchScope.getProject();
        if (project != null) {
            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
            PsiClass listenerConfiguration = javaPsiFacade.findClass(className, searchScope);
            if (listenerConfiguration == null) {
                System.out.println("not found class with name: " + className);
                return Collections.emptyList();
            }
            return methodNames.stream()
                    .map(methodName -> listenerConfiguration.findMethodsByName(methodName, false))
                    .flatMap(Stream::of)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
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

    public static String getQuelifierClassName(PsiMethodCallExpression methodCallExpression) {
        PsiReferenceExpression methodExpression = methodCallExpression.getMethodExpression();
        PsiExpression qualifierExpression = methodExpression.getQualifierExpression();
        if (qualifierExpression != null) {
            PsiType type = qualifierExpression.getType();
            if (type != null) {
                return type.getCanonicalText();
            }
        }
        return null;
    }

}
