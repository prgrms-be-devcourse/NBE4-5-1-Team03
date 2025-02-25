package com.shop.coffee.global.config;

import com.shop.coffee.item.entity.Item;
import com.shop.coffee.item.repository.ItemRepository;
import com.shop.coffee.order.OrderStatus;
import com.shop.coffee.order.entity.Order;
import com.shop.coffee.order.repository.OrderRepository;
import com.shop.coffee.orderitem.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import org.springframework.context.annotation.Profile;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    @Bean
    @Profile("local")
    public CommandLineRunner initLocalData() {
        return args -> {

            //주문이 한개라도 db에 남아있다면 초기화하지 않음
            if (orderRepository.count() > 0)
                return;

            // 상품 생성
            Item item1 = new Item("Brazil Serra Do Caparao", "Category1", 100, "Description1", "BrazilSerraDoCaparao.png");
            Item item2 = new Item("Columbia Narino", "Category2", 200, "Description2", "ColumbiaNarino.png");
            Item item3 = new Item("Columbia Quindio", "Category3", 300, "Description3", "ColumbiaQuindio.png");
            Item item4 = new Item("Ethiopia Sidamo", "Category4", 400, "Description4", "EthiopiaSidamo.png");

            itemRepository.saveAll(List.of(item1, item2, item3, item4));

            // 주문 생성
            Order order1 = new Order("user1@example.com", "Address1", "Zipcode1", OrderStatus.RECEIVED, 400, null);
            Order order2 = new Order("user2@example.com", "Address2", "Zipcode2", OrderStatus.SHIPPING, 700, null);
            Order order3 = new Order("user3@example.com", "Address3", "Zipcode3", OrderStatus.RECEIVED, 1000, null);
            Order order4 = new Order("user4@example.com", "Address4", "Zipcode4", OrderStatus.SHIPPING, 900, null);
            // 주문 생성 - 동일한 이메일 주문
            Order order5 = new Order("user5@example.com", "Address5", "Zipcode5", OrderStatus.RECEIVED, 100, null);
            Order order6 = new Order("user5@example.com", "Address5", "Zipcode5", OrderStatus.RECEIVED, 500, null); //totalPrice가 하드코딩되어있음
            Order order7 = new Order("user5@example.com", "Address5", "Zipcode5", OrderStatus.SHIPPING, 900, null);
            Order order8 = new Order("user5@example.com", "Address5", "Zipcode5", OrderStatus.SHIPPING, 600, null);
            Order order9 = new Order("user5@example.com", "Address5", "Zipcode5", OrderStatus.SHIPPING, 1000, null);

            // 주문 아이템 생성 및 주문에 추가
            order1.setOrderItems(List.of(new OrderItem(order1, item1, 100, 2, item1.getImagePath()), new OrderItem(order1, item2, 200, 1, item2.getImagePath())));
            order2.setOrderItems(List.of(new OrderItem(order2, item2, 200, 2, item2.getImagePath()), new OrderItem(order2, item3, 300, 1, item3.getImagePath())));
            order3.setOrderItems(List.of(new OrderItem(order3, item3, 300, 2, item3.getImagePath()), new OrderItem(order3, item4, 400, 1, item4.getImagePath())));
            order4.setOrderItems(List.of(new OrderItem(order4, item4, 400, 2, item4.getImagePath()), new OrderItem(order4, item1, 100, 1, item1.getImagePath())));
            // 주문 아이템 생성 및 주문에 추가 - 동일한 이메일 주문
            //1번 1개 RECEIVED 총 100
            order5.setOrderItems(List.of(new OrderItem(order5, item1, 100, 1, "BrazilSerraDoCaparao.png")));
            //2번1개 3번 1개 RECEIVED 총 200+300=500
            order6.setOrderItems(List.of(new OrderItem(order6, item2, 200, 1, "ColumbiaNarino.png"), new OrderItem(order6, item3, 300, 1, "ColumbiaQuindio.png")));
            //1번1개 2번1개 3번 2개 SHIPPING 총 100+200+300*2=900
            order7.setOrderItems(List.of(new OrderItem(order7, item1, 100, 1, "BrazilSerraDoCaparao.png"), new OrderItem(order7, item2, 200, 1, "ColumbiaNarino.png"), new OrderItem(order7, item3, 300, 2, "ColumbiaQuindio.png")));
            //3번 2개 SHIPPING 총 300*2=600
            order8.setOrderItems(List.of(new OrderItem(order8, item3, 300, 2, "ColumbiaQuindio.png")));
            //1번 1개 2번 1개 3번 1개 4번 1개 SHIPPING 총 100+200+300+400=1000
            order9.setOrderItems(List.of(new OrderItem(order9, item1, 100, 1, "BrazilSerraDoCaparao.png"), new OrderItem(order9, item2, 200, 1, "ColumbiaNarino.png"), new OrderItem(order9, item3, 300, 1, "ColumbiaQuindio.png"), new OrderItem(order9, item4, 400, 1, "EthiopiaSidamo.png")));


            orderRepository.saveAll(List.of(order1, order2, order3, order4,order5,order6,order7,order8,order9));
        };
    }

    @Bean
    @Profile("prod")
    public CommandLineRunner initProdData() {
        return args -> {

            if (itemRepository.count() > 0)
                return;

            // 상품 생성
            Item item1 = new Item("Columbia Narino", "커피콩", 5000, "콜롬비아 남서부의 고산지대, 해발 2,000m 이상의 높은 지대에서 자란 나리뇨 원두는 풍부한 일조량과 낮은 기온 덕분에 신선한 산미와 달콤한 과일향을 고스란히 담고 있습니다.\n"
                    + "첫 모금에는 상큼한 과일의 향긋함이 퍼지고, 이어지는 은은한 초콜릿 향은 부드러운 목넘김과 함께 입안 가득 기분 좋은 여운을 남깁니다. 산미와 단맛의 조화를 선호하는 분께 특히 추천드리며, 부담 없이 즐길 수 있는 커피입니다.", "ColumbiaNarino.png");
            Item item2 = new Item("Brazil Serra Do Caparao", "커피콩", 10000, "브라질 남동부의 세하 두 카파라오 지역은 따뜻한 기후와 비옥한 토양으로 커피 재배에 최적의 환경을 제공합니다.\n"
                    + "그곳에서 자란 이 원두는 한 모금 머금는 순간 입안을 채우는 견과류와 다크 초콜릿의 고소함이 인상적이며, 뒤이어 부드럽게 스며드는 카라멜 같은 달콤한 여운이 기분 좋은 마무리를 선사합니다.", "BrazilSerraDoCaparao.png");
            Item item3 = new Item("Columbia Quindio", "커피콩", 15000, "콜롬비아 킨디오 지역에서 수확된 이 원두는 특별히 화이트 와인 발효 과정을 거쳐 독창적인 풍미를 완성합니다.\n"
                    + "포도와 사과 같은 상큼한 과일향과 화이트 와인을 연상케 하는 산미가 부드러운 질감과 조화를 이루며, 미디엄 바디의 목넘김은 기분 좋은 여운을 오래도록 남깁니다.\n"
                    + "평소 특별한 커피 경험을 찾으시는 분이나 독특한 향미를 즐기고 싶으신 분께 적극 추천드립니다. 핸드드립으로 추출하면 그 깊고 풍부한 맛을 가장 잘 느낄 수 있습니다.", "ColumbiaQuindio.png");
            Item item4 = new Item("Ethiopia Sidamo", "커피콩", 20000, "커피의 기원지로 알려진 에티오피아 시다모 지역에서 재배된 이 원두는 고도와 기후의 조화로 인해 독보적인 향미를 자랑합니다.\n"
                    + "입안에 퍼지는 화사한 꽃향기와 블루베리, 복숭아를 연상케 하는 상큼한 과일향은 마시는 순간 기분을 환하게 밝혀줍니다. 산뜻하고 밝은 산미에 가벼운 바디감이 더해져 첫 모금부터 끝까지 상쾌함이 이어지며, 특히 핸드드립이나 아이스 커피로 즐길 때 그 풍미가 더욱 살아납니다.", "EthiopiaSidamo.png");

            itemRepository.saveAll(List.of(item1, item2, item3, item4));

        };
    }
}