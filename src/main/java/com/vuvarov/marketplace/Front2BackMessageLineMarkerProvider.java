package com.vuvarov.marketplace;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.impl.source.tree.java.PsiMethodCallExpressionImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.vuvarov.marketplace.strategy.calculator.ArgumentValueCalculator;
import com.vuvarov.marketplace.strategy.creator.MarkerInfoCreator;
import com.vuvarov.marketplace.strategy.creator.ToListenerMarkerInfoCreator;
import com.vuvarov.marketplace.strategy.creator.ToSenderMarkerInfoCreator;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

import static com.vuvarov.marketplace.strategy.target.searcher.ListenersSearcher.BASE_JMS_LISTENER_CLASS_NAME;
import static com.vuvarov.marketplace.strategy.target.searcher.ListenersSearcher.LISTENER_METHODS;
import static com.vuvarov.marketplace.strategy.target.searcher.SendersSearcher.AMQUTIL_CLASS_NAME;
import static com.vuvarov.marketplace.strategy.target.searcher.SendersSearcher.SENDER_METHODS;
import static com.vuvarov.marketplace.strategy.target.searcher.TargetSearcher.MQENTITY_ARGUMENT_INDEX;
import static com.vuvarov.marketplace.strategy.target.searcher.TargetSearcher.MQOPERATION_ARGUMENT_INDEX;
import static com.vuvarov.marketplace.util.PsiCommonUtil.getCallMethodName;
import static com.vuvarov.marketplace.util.PsiCommonUtil.getClassByName;
import static com.vuvarov.marketplace.util.PsiCommonUtil.getQuelifierClass;
import static com.vuvarov.marketplace.util.PsiCommonUtil.getScope;

public class Front2BackMessageLineMarkerProvider extends RelatedItemLineMarkerProvider {

    private final Logger LOG = Logger.getInstance(this.getClass());
    private static final MarkerInfoCreator toListenerMarkerCreator = new ToListenerMarkerInfoCreator();
    private static final MarkerInfoCreator toSenderMarkerCreator = new ToSenderMarkerInfoCreator();
    private static final ArgumentValueCalculator calculator = new ArgumentValueCalculator();

    public Front2BackMessageLineMarkerProvider() {
        System.out.println("Front2BackMessageLineMarkerProvider created");
        LOG.info("Front2BackMessageLineMarkerProvider created");
    }

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {
        if (!(element instanceof PsiMethodCallExpression)) return;

        PsiClass quelifierClass = getQuelifierClass((PsiMethodCallExpression) element);
        if (quelifierClass != null) {
            String qualifierType = quelifierClass.getQualifiedName();
            String methodName = getCallMethodName((PsiMethodCallExpressionImpl) element);
            if (isSenderMethod(qualifierType, methodName)) {
                addMarker((PsiMethodCallExpression) element, result, toListenerMarkerCreator);
            } else if (isListenerMethod(element, quelifierClass, methodName)) {
                addMarker((PsiMethodCallExpression) element, result, toSenderMarkerCreator);
            }
        }
    }

    private void addMarker(@NotNull PsiMethodCallExpression element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result, MarkerInfoCreator toSenderMarkerCreator) {
        Set<String> mqEntitys = calculator.argumentValues(element, MQENTITY_ARGUMENT_INDEX);
        Set<String> mqOperations = calculator.argumentValues(element, MQOPERATION_ARGUMENT_INDEX);
        if (!mqEntitys.isEmpty() && !mqOperations.isEmpty()) {
            result.add(toSenderMarkerCreator.createLineMarkerInfo(element));
        }
    }

    private boolean isListenerMethod(PsiElement element, PsiClass quelifierClass, String methodName) {
        GlobalSearchScope scope = getScope(element);
        if (scope == null) return false;
        PsiClass abstractJmsListenerClass = getClassByName(scope, BASE_JMS_LISTENER_CLASS_NAME); // todo возможно стоит получатьодин раз при инициализации или кэшировать как-то
        return quelifierClass != null && abstractJmsListenerClass != null &&
                quelifierClass.isInheritor(abstractJmsListenerClass, true) &&
                LISTENER_METHODS.contains(methodName);
    }

    private boolean isSenderMethod(String qualifierType, String methodName) {
        return AMQUTIL_CLASS_NAME.equals(qualifierType) && SENDER_METHODS.contains(methodName);
    }
}
