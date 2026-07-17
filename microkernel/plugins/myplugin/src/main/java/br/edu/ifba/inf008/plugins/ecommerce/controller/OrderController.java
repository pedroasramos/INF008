package br.edu.ifba.inf008.plugins.ecommerce.controller;

import br.edu.ifba.inf008.plugins.ecommerce.discount.CouponDiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.discount.DiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.discount.StudentDiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.model.Order;
import br.edu.ifba.inf008.plugins.ecommerce.payment.BankSlipPayment;
import br.edu.ifba.inf008.plugins.ecommerce.payment.CreditCardPayment;
import br.edu.ifba.inf008.plugins.ecommerce.payment.Payable;
import br.edu.ifba.inf008.plugins.ecommerce.payment.PixPayment;
import br.edu.ifba.inf008.plugins.ecommerce.service.OrderService;
import br.edu.ifba.inf008.plugins.ecommerce.shipping.ExpressShippingPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.shipping.PickupShippingPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.shipping.ShippingPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.shipping.StandardShinppingPolicy;

import java.util.List;

public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    public void createOrderWithCoupon(int cartId, String coupon, String shippingType,
                                      Payable paymentMethod) {
        DiscountPolicy discountPolicy = new CouponDiscountPolicy(coupon);
        ShippingPolicy shippingPolicy = resolveShippingPolicy(shippingType);
        orderService.createOrder(cartId, discountPolicy, shippingPolicy, paymentMethod);
    }

    public void createOrderWithStudentDiscount(int cartId, String shippingType,
                                               Payable paymentMethod) {
        DiscountPolicy discountPolicy = new StudentDiscountPolicy();
        ShippingPolicy shippingPolicy = resolveShippingPolicy(shippingType);
        orderService.createOrder(cartId, discountPolicy, shippingPolicy, paymentMethod);
    }

    public Payable buildCreditCardPayment(long cardNumber, String holder, int cvv, String expirationDate) {
        return new CreditCardPayment(cardNumber, holder, cvv, expirationDate);
    }

    public Payable buildPixPayment(String pixKey) {
        return new PixPayment(pixKey);
    }

    public Payable buildBankSlipPayment(String barcode, String dueDate) {
        return new BankSlipPayment(barcode, dueDate);
    }

    public Order getOrder(int orderId) {
        return orderService.findById(orderId);
    }

    public List<Order> listOrders() {
        return orderService.findAll();
    }

    public void cancelOrder(int orderId) {
        orderService.cancelOrder(orderId);
    }

    public void deleteOrder(int orderId) {
        orderService.remove(orderId);
    }

    private ShippingPolicy resolveShippingPolicy(String type) {
        switch (type) {
            case "EXPRESS": return new ExpressShippingPolicy();
            case "PICKUP": return new PickupShippingPolicy();
            case "STANDARD": return new StandardShinppingPolicy();
            default: throw new IllegalArgumentException("Invalid shipping type: " + type);
        }
    }
}
