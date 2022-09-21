package com.hoody.audience.view;

import com.hoody.annotation.model.IModel;
import com.hoody.audience.view.fragment.ILivePlayContentProducer;

public interface IPlayContentProducerRegister extends IModel {
    void registerPlayContentProducer(ILivePlayContentProducer livePlayContentProducer);

    ILivePlayContentProducer getPlayContentProducer();
}
