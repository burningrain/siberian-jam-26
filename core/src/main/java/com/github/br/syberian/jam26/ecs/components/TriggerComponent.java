package com.github.br.syberian.jam26.ecs.components;

import com.artemis.Component;

public class TriggerComponent extends Component {

    public enum UseType {
        ONE_TIME,         // Срабатывает один раз и удаляется из игры
        PERSISTENCE,      // Срабатывает КАЖДЫЙ КАДР, пока игрок внутри
        ON_ENTER_ONCE     // Срабатывает 1 раз при входе. Пока игрок внутри — молчит. Сброс при выходе.
    }
    public enum ConditionType {ON_ENTER, ON_KEY_E}


    public UseType useType;
    public ConditionType conditionType;
    public TriggerAction action;

}
