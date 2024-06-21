package com.plotsquared.fabric.data;

import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotId;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;

public class PlotSquaredDataAttachments {

    public static AttachmentType<Plot> PLOT_DATA;
    public static AttachmentType<GlobalPos> P2;
    public static AttachmentType<PlotId> SHULKER_PLOT;

    static {
        PLOT_DATA = AttachmentRegistry.create(new ResourceLocation("plotsquared", "plot_data"));
        P2 = AttachmentRegistry.create(new ResourceLocation("plotsquared", "p2"));
        SHULKER_PLOT = AttachmentRegistry.create(new ResourceLocation("plotsquared", "shulkerPlot"));
    }



}
