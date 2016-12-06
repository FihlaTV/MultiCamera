package com.st.android.device.camera;

import java.nio.ByteBuffer;

/**
 * Created by SeungTaek.Lim on 16. 7. 15..
 */
public interface ImageMap {
    public ByteBuffer get(byte[] frame);
    public void addBuffer(byte[] array);
}
