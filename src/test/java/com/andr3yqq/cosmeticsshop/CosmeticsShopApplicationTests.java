package com.andr3yqq.cosmeticsshop;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "AUTH0_DOMAIN=https://test-auth0-domain/",
    "AUTH0_AUDIENCE=test-audience"
})
class CosmeticsShopApplicationTests {

    @Test
    void contextLoads() {
    }

}
