package com.vuvarov.marketplace.strategy.target.searcher;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;

import java.util.Arrays;
import java.util.List;

import static com.vuvarov.marketplace.util.PsiCommonUtil.findMethods;
import static com.vuvarov.marketplace.util.PsiCommonUtil.getScope;

public class ListenersSearcher extends TargetSearcher {
    public static final String JMS_LISTENER_CLASS_NAME = "ru.openbank.marketplace.util.configuration.listener.JsonJmsListener";
    public static final List<String> LISTENER_METHODS = Arrays.asList("configure", "configureProducer");

    @Override
    protected List<PsiMethod> getPotencialMethods(PsiElement element) {
        return findMethods(getScope(element), JMS_LISTENER_CLASS_NAME, LISTENER_METHODS);
    }
}
