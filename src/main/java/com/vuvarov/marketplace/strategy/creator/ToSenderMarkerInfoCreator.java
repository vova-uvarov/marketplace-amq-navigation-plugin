package com.vuvarov.marketplace.strategy.creator;

import com.intellij.psi.PsiElement;
import com.vuvarov.marketplace.MarketPlaceIcons;
import com.vuvarov.marketplace.strategy.target.searcher.ListenersSearcher;
import com.vuvarov.marketplace.strategy.target.searcher.SendersSearcher;
import com.vuvarov.marketplace.strategy.target.searcher.TargetSearcher;

import javax.swing.*;
import java.util.Collection;

public class ToSenderMarkerInfoCreator implements MarkerInfoCreator {

    private static final TargetSearcher targetSearcher = new SendersSearcher();

    @Override
    public String getTooltipText() {
        return "Navigate to Senders";
    }

    @Override
    public Icon getIcon() {
        return MarketPlaceIcons.LISTENER;
    }

    @Override
    public Collection<? extends PsiElement> searchTargets(PsiElement element) {
        return targetSearcher.searchTargets(element);
    }
}
