package org.fairy.mc.title;

import org.fairy.mc.message.MCMessage;

public class MCTitle {

    private MCMessage title;
    private MCMessage subTitle;
    private int fadeIn, stay, fadeOut;

    public MCTitle() {
        this.title = MCMessage.text("");
        this.subTitle = MCMessage.text("");
        this.fadeIn = 20;
        this.stay = 200;
        this.fadeOut = 20;
    }

    public MCTitle(MCMessage title) {
        this.title = title;
        this.subTitle = MCMessage.text("");
        this.fadeIn = 20;
        this.stay = 200;
        this.fadeOut = 20;
    }

    public MCTitle(MCMessage title, MCMessage subTitle) {
        this.title = title;
        this.subTitle = subTitle;
        this.fadeIn = 20;
        this.stay = 200;
        this.fadeOut = 20;
    }

    public MCTitle(MCMessage title, MCMessage subTitle, int fadeIn, int stay, int fadeOut) {
        this.title = title;
        this.subTitle = subTitle;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    public MCTitle title(MCMessage title) {
        this.title = title;
        return this;
    }

    public MCTitle subTitle(MCMessage subTitle) {
        this.subTitle = subTitle;
        return this;
    }

    public MCTitle fadeIn(int fadeIn) {
        this.fadeIn = fadeIn;
        return this;
    }

    public MCTitle stay(int stay) {
        this.stay = stay;
        return this;
    }

    public MCTitle fadeOut(int fadeOut) {
        this.fadeOut = fadeOut;
        return this;
    }

}
