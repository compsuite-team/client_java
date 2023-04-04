package io.prometheus.expositionformat;

import io.prometheus.com_google_protobuf_3_21_7.Timestamp;

public class ProtobufUtil {

    public static Timestamp timestampFromMillis(long timestampMillis) {
        return Timestamp.newBuilder()
                .setSeconds(timestampMillis / 1000L)
                .setNanos((int) (timestampMillis % 1000L * 1000000L))
                .build();
    }
}
