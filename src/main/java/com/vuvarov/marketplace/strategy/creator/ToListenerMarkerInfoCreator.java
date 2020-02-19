package com.vuvarov.marketplace.strategy.creator;

import com.intellij.psi.PsiElement;
import com.vuvarov.marketplace.MarketPlaceIcons;
import com.vuvarov.marketplace.strategy.target.searcher.ListenersSearcher;
import com.vuvarov.marketplace.strategy.target.searcher.TargetSearcher;

import javax.swing.*;
import java.util.Collection;

public class ToListenerMarkerInfoCreator implements MarkerInfoCreator {

    private static final TargetSearcher targetSearcher = new ListenersSearcher();

    @Override
    public String getTooltipText() {
        return "Navigate to Listeners";
    }

    @Override
    public Icon getIcon() {
        return MarketPlaceIcons.SENDER;
    }

    @Override
    public Collection<? extends PsiElement> searchTargets(PsiElement element) {
        return targetSearcher.searchTargets(element);
    }
}
