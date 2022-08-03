package fyi.karm.perimeter;

import android.content.Context;

import com.getcapacitor.JSObject;

import java.util.ArrayList;

public final class SimplePerimeterReceiver extends PerimeterReceiver {
    @Override
    public void onFenceTriggered(Context context, ArrayList<JSObject> triggeredJSFences, long triggerTime, int transitionType) {

    }

    @Override
    public void onError(Context context, int errorCode, String errorMessage) {

    }
}
