package com.vuvarov.marketplace;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiEnumConstant;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiReturnStatement;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.tree.java.PsiMethodCallExpressionImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Front2BackMessageLineMarkerProvider extends RelatedItemLineMarkerProvider {

    //    todo вынести в настройки
    private static final String AMQUTIL_CLASS_NAME = "ru.openbank.marketplace.util.AMQUtil";
    private static final String JMS_LISTENER_CLASS_NAME = "ru.openbank.marketplace.util.configuration.listener.JsonJmsListener";
    private static final List<String> LISTENER_METHODS = Arrays.asList("configure", "configureProducer");
    private static final List<String> SENDER_METHODS = Arrays.asList("simpleSendAndReceive", "sendAndReceiveMessage", "sendAndReceiveMessageAsync", "sendMessage", "sendMessageWithDelay");
    private static final int MQENTITY_ARGUMENT_INDEX = 0;
    private static final int MQOPERATION_ARGUMENT_INDEX = 1;

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {
        // This must be an element with a literal expression as a parent
        if (!(element instanceof PsiMethodCallExpression)) return;
        String qualifierType = getQualifierType((PsiMethodCallExpression) element);
        String methodName = getMethodName((PsiMethodCallExpressionImpl) element);
        if (AMQUTIL_CLASS_NAME.equals(qualifierType) && SENDER_METHODS.contains(methodName)) {
            String mqEntity = argumentValue((PsiMethodCallExpression) element, MQENTITY_ARGUMENT_INDEX);
            if (mqEntity != null) {
                NavigationGutterIconBuilder<PsiElement> gutterIcon = NavigationGutterIconBuilder.create(MarketPlaceIcons.SENDER)
                        .setTargets(NotNullLazyValue.createValue(() -> searchListeners(element)))
                        .setTooltipText("Navigate to Listeners");
                result.add(gutterIcon.createLineMarkerInfo(element));
            }
        }

        if (qualifierType != null && qualifierType.startsWith(JMS_LISTENER_CLASS_NAME) && LISTENER_METHODS.contains(methodName)) {
            String mqEntity = argumentValue((PsiMethodCallExpression) element, MQENTITY_ARGUMENT_INDEX);
            if (mqEntity != null) {
                NavigationGutterIconBuilder<PsiElement> gutterIcon = NavigationGutterIconBuilder.create(MarketPlaceIcons.LISTENER)
                        .setTargets(NotNullLazyValue.createValue(() -> searchSenders(element)))
                        .setTooltipText("Navigate to Sender");
                result.add(gutterIcon.createLineMarkerInfo(element));
            }
        }

    }

    private String getMethodName(@NotNull PsiMethodCallExpressionImpl element) {
        return element.getMethodExpression().getReferenceName();
    }

    @NotNull
    private Collection<? extends PsiElement> searchSenders(@NotNull PsiElement element) {
        List<PsiMethod> senderMethods = getPsiMethods(element, AMQUTIL_CLASS_NAME, SENDER_METHODS);
        List<PsiElement> resultElements = new ArrayList<>();
        String sendMqEntity = argumentValue((PsiMethodCallExpression) element, MQENTITY_ARGUMENT_INDEX);
        String sendMqOperation = argumentValue((PsiMethodCallExpression) element, MQOPERATION_ARGUMENT_INDEX);

        for (PsiMethod method : senderMethods) {
            Query<PsiReference> search = MethodReferencesSearch.search(method);
            List<PsiElement> collect = search.findAll().stream()
                    .filter(searchResult -> isListener(sendMqEntity, sendMqOperation, searchResult))
                    .map(PsiReference::getElement)
                    .collect(Collectors.toList());
            resultElements.addAll(collect);
        }
        return resultElements;
    }

    private Collection<? extends PsiElement> searchListeners(@NotNull PsiElement element) {
        List<PsiMethod> listenerMethods = getPsiMethods(element, JMS_LISTENER_CLASS_NAME, LISTENER_METHODS);
        List<PsiElement> resultElements = new ArrayList<>();
        String sendMqEntity = argumentValue((PsiMethodCallExpression) element, MQENTITY_ARGUMENT_INDEX);
        String sendMqOperation = argumentValue((PsiMethodCallExpression) element, MQOPERATION_ARGUMENT_INDEX);

        for (PsiMethod method : listenerMethods) {
            Query<PsiReference> search = MethodReferencesSearch.search(method);
            List<PsiElement> collect = search.findAll().stream()
                    .filter(searchResult -> isListener(sendMqEntity, sendMqOperation, searchResult))
                    .map(PsiReference::getElement)
                    .collect(Collectors.toList());
            resultElements.addAll(collect);
        }
        return resultElements;
    }

    private boolean isListener(String sendMqEntity, String sendMqOperation, PsiReference searchResult) {
        PsiElement searchResultElement = searchResult.getElement().getParent();
        String mqEntity = argumentValue((PsiMethodCallExpression) searchResultElement, MQENTITY_ARGUMENT_INDEX);
        String mqOperation = argumentValue((PsiMethodCallExpression) searchResultElement, MQOPERATION_ARGUMENT_INDEX);
        return Objects.equals(mqEntity, sendMqEntity) && Objects.equals(mqOperation, sendMqOperation);
    }

    private String getQualifierType(PsiMethodCallExpression methodCallExpression) {
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


    private List<PsiMethod> getPsiMethods(PsiElement element, String className, List<String> methodNames) {
        Module module = ModuleUtilCore.findModuleForPsiElement(element);
        if (module != null) {
            GlobalSearchScope scope = module.getModuleScope(false);
            Project project = scope.getProject();
            if (project != null) {
                JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
                PsiClass listenerConfiguration = javaPsiFacade.findClass(className, scope);
                if (listenerConfiguration == null) {
                    System.out.println("not found class with name: " + className);
                    return Collections.emptyList();
                }
                return methodNames.stream()
                        .map(methodName -> listenerConfiguration.findMethodsByName(methodName, false))
                        .flatMap(Stream::of)
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    private String argumentValue(PsiMethodCallExpression methodCallExpression, int index) {
        PsiExpression argumentExpression = methodCallExpression.getArgumentList().getExpressions()[index];
        String expressionValue = valueFromExpression(argumentExpression);
        if (expressionValue != null) {
            return expressionValue;
        }
        if (argumentExpression instanceof PsiMethodCallExpression) {
            PsiMethod psiMethod = ((PsiMethodCallExpression) argumentExpression).resolveMethod();
            if (psiMethod != null) {
                PsiStatement[] statements = psiMethod.getBody().getStatements();
                PsiReturnStatement returnStatement = (PsiReturnStatement) Arrays.stream(statements)
                        .filter(s -> s instanceof PsiReturnStatement)
                        .findAny()
                        .orElse(null);

                if (returnStatement != null) {
                    PsiExpression returnValue = returnStatement.getReturnValue();
                    return valueFromExpression(returnValue);
                }
            }
        }
        return null;
    }

    private String valueFromExpression(PsiExpression expression) {
        PsiReference reference = expression.getReference();
        if (reference != null) {
            PsiElement resolvedElement = reference.resolve();
            if (resolvedElement instanceof PsiEnumConstant) {
                return ((PsiEnumConstant) resolvedElement).getName();
            }
        }
        return null;
    }
}
