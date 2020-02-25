package com.vuvarov.marketplace.strategy.target.searcher;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.vuvarov.marketplace.util.PsiCommonUtil.findMethods;
import static com.vuvarov.marketplace.util.PsiCommonUtil.getScope;

public class ListenersSearcher extends TargetSearcher {
    public static final String BASE_JMS_LISTENER_CLASS_NAME = "ru.openbank.marketplace.util.configuration.listener.AbstractJmsListener";
    //    todo тут по хорошему просто получить всех наследников для BASE_JMS_LISTENER_CLASS_NAME. Пока не разобрался как
    public static final List<String> LISTENER_METHODS = Arrays.asList("configure", "configureProducer");

    private static final List<String> JMS_LISTENER_CLASSES = Arrays.asList(BASE_JMS_LISTENER_CLASS_NAME,
            "ru.openbank.marketplace.util.configuration.listener.JsonJmsListener",
            "ru.openbank.marketplace.util.configuration.listener.CreateUpdateJmsListener");

    @Override
    protected List<PsiMethod> getPotencialMethods(PsiElement element) {
        GlobalSearchScope scope = getScope(element);
        return JMS_LISTENER_CLASSES.stream()
                .map(c->findMethods(scope, c, LISTENER_METHODS))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
