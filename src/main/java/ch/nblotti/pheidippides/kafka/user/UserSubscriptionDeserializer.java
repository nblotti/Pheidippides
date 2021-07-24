package ch.nblotti.pheidippides.kafka.user;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class UserSubscriptionDeserializer implements Deserializer<UserSubscription> {



    private final ModelMapper modelMapper = new ModelMapper();


    public UserSubscriptionDeserializer() {
        initMapper();
    }

    @Override
    public UserSubscription deserialize(String s, byte[] bytes) {

        return modelMapper.map(bytes, UserSubscription.class);
    }

    private void initMapper() {
        Converter<byte[], UserSubscription> userConverter = new AbstractConverter<byte[], UserSubscription>() {

            @Override
            protected UserSubscription convert(byte[] bytes) {

                DocumentContext context = JsonPath.parse(new String(bytes));
                List<String> stocks  = context.read("$.stocks[*]", List.class);
                List<String> etfs  = context.read("$.etfs[*]", List.class);
                UserSubscription userSubscription = new UserSubscription();
                userSubscription.setStocks(stocks);
                userSubscription.setEtfs(etfs);
                return userSubscription;
            }
        };
        modelMapper.addConverter(userConverter);
    }
}
