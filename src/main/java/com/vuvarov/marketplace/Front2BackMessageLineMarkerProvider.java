package com.vuvarov.marketplace;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.impl.source.tree.java.PsiMethodCallExpressionImpl;
import com.vuvarov.marketplace.strategy.calculator.ArgumentValueCalculator;
import com.vuvarov.marketplace.strategy.creator.MarkerInfoCreator;
import com.vuvarov.marketplace.strategy.creator.ToListenerMarkerInfoCreator;
import com.vuvarov.marketplace.strategy.creator.ToSenderMarkerInfoCreator;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

import static com.vuvarov.marketplace.strategy.target.searcher.ListenersSearcher.JMS_LISTENER_CLASS_NAME;
import static com.vuvarov.marketplace.strategy.target.searcher.ListenersSearcher.LISTENER_METHODS;
import static com.vuvarov.marketplace.strategy.target.searcher.SendersSearcher.AMQUTIL_CLASS_NAME;
import static com.vuvarov.marketplace.strategy.target.searcher.SendersSearcher.SENDER_METHODS;
import static com.vuvarov.marketplace.strategy.target.searcher.TargetSearcher.MQENTITY_ARGUMENT_INDEX;
import static com.vuvarov.marketplace.util.PsiCommonUtil.getCallMethodName;
import static com.vuvarov.marketplace.util.PsiCommonUtil.getQuelifierClassName;

public class Front2BackMessageLineMarkerProvider extends RelatedItemLineMarkerProvider {

    private static final MarkerInfoCreator toListenerMarkerCreator = new ToListenerMarkerInfoCreator();
    private static final MarkerInfoCreator toSenderMarkerCreator = new ToSenderMarkerInfoCreator();
    private static final ArgumentValueCalculator calculator = new ArgumentValueCalculator();

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {
        if (!(element instanceof PsiMethodCallExpression)) return;

        String qualifierType = getQuelifierClassName((PsiMethodCallExpression) element);
        String methodName = getCallMethodName((PsiMethodCallExpressionImpl) element);
        if (isSenderMethod(qualifierType, methodName)) {

            Set<String> mqEntity = calculator.argumentValues((PsiMethodCallExpression) element, MQENTITY_ARGUMENT_INDEX);
            if (!mqEntity.isEmpty()) {
                result.add(toListenerMarkerCreator.createLineMarkerInfo((PsiMethodCallExpression) element));
            }
        }

        if (isListenerMethod(qualifierType, methodName)) {
            Set<String> mqEntity = calculator.argumentValues((PsiMethodCallExpression) element, MQENTITY_ARGUMENT_INDEX);
            if (!mqEntity.isEmpty()) {
                result.add(toSenderMarkerCreator.createLineMarkerInfo((PsiMethodCallExpression) element));
            }
        }
    }

    private boolean isListenerMethod(String qualifierType, String methodName) {
        return qualifierType != null && qualifierType.startsWith(JMS_LISTENER_CLASS_NAME) && LISTENER_METHODS.contains(methodName);
    }

    private boolean isSenderMethod(String qualifierType, String methodName) {
        return AMQUTIL_CLASS_NAME.equals(qualifierType) && SENDER_METHODS.contains(methodName);
    }
}
