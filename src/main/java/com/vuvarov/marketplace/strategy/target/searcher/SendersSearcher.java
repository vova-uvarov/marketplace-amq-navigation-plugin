package com.vuvarov.marketplace.strategy.target.searcher;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.Arrays;
import java.util.List;

import static com.vuvarov.marketplace.util.PsiCommonUtil.*;

public class SendersSearcher extends TargetSearcher {
    public static final String AMQUTIL_CLASS_NAME = "ru.openbank.marketplace.util.AMQUtil";
    public static final List<String> SENDER_METHODS = Arrays.asList("simpleSendAndReceive", "sendAndReceiveMessage", "sendAndReceiveMessageAsync", "sendMessage", "sendMessageWithDelay");

    @Override
    protected List<PsiMethod> getPotencialMethods(PsiElement element) {
        GlobalSearchScope scope = getScope(element);
        return findMethods(scope, getClassByName(scope, AMQUTIL_CLASS_NAME), SENDER_METHODS);
    }
}
