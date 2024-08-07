import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

public class WeatherDeserializationSchema implements DeserializationSchema<Weather> {
    private static final long serialVersionUID = 1L;

    private transient ObjectMapper objectMapper;

    @Override
    public void open(InitializationContext context) {
        objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
    }

    @Override
    public Weather deserialize(byte[] message) throws IOException {
        return objectMapper.readValue(message, Weather.class);
    }

    @Override
    public boolean isEndOfStream(Weather nextElement) {
        return false;
    }

    @Override
    public TypeInformation<Weather> getProducedType() {
        return Types.POJO(Weather.class);
    }
}
