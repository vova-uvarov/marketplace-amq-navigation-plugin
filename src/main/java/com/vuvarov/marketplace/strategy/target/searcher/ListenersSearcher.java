package com.vuvarov.marketplace.strategy.target.searcher;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.util.Query;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.vuvarov.marketplace.util.PsiCommonUtil.*;

public class ListenersSearcher extends TargetSearcher {
    public static final String BASE_JMS_LISTENER_CLASS_NAME = "ru.openbank.marketplace.util.configuration.listener.AbstractJmsListener";
    public static final List<String> LISTENER_METHODS = Arrays.asList("configure", "configureProducer", "configureConsumer");

    @Override
    protected List<PsiMethod> getPotencialMethods(PsiElement element) {
        GlobalSearchScope scope = getScope(element);

        PsiClass baseJmsClass = getClassByName(scope, BASE_JMS_LISTENER_CLASS_NAME);
        Query<PsiClass> inheritors = ClassInheritorsSearch.search(baseJmsClass);

        return Stream.concat(Stream.of(baseJmsClass), inheritors.findAll().stream())
                .map(c -> findMethods(scope, c, LISTENER_METHODS))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
