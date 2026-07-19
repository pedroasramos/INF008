package br.edu.ifba.inf008.plugins;

import br.edu.ifba.inf008.interfaces.IPlugin;
import br.edu.ifba.inf008.interfaces.ICore;
import br.edu.ifba.inf008.interfaces.IUIController;

import br.edu.ifba.inf008.plugins.ecommerce.controller.CartController;
import br.edu.ifba.inf008.plugins.ecommerce.controller.CustomerController;
import br.edu.ifba.inf008.plugins.ecommerce.controller.OrderController;
import br.edu.ifba.inf008.plugins.ecommerce.controller.ProductController;
import br.edu.ifba.inf008.plugins.ecommerce.repository.CartRepository;
import br.edu.ifba.inf008.plugins.ecommerce.repository.CartRepositoryImp;
import br.edu.ifba.inf008.plugins.ecommerce.repository.CustomerRepository;
import br.edu.ifba.inf008.plugins.ecommerce.repository.CustomerRepositoryImp;
import br.edu.ifba.inf008.plugins.ecommerce.repository.OrderRepository;
import br.edu.ifba.inf008.plugins.ecommerce.repository.OrderRepositoryImp;
import br.edu.ifba.inf008.plugins.ecommerce.repository.ProductRepository;
import br.edu.ifba.inf008.plugins.ecommerce.repository.ProductRepositoryImp;
import br.edu.ifba.inf008.plugins.ecommerce.service.CartService;
import br.edu.ifba.inf008.plugins.ecommerce.service.CustomerService;
import br.edu.ifba.inf008.plugins.ecommerce.service.OrderService;
import br.edu.ifba.inf008.plugins.ecommerce.service.ProductService;
import br.edu.ifba.inf008.plugins.ecommerce.view.CheckoutView;
import br.edu.ifba.inf008.plugins.ecommerce.view.CustomerView;
import br.edu.ifba.inf008.plugins.ecommerce.view.OrderView;
import br.edu.ifba.inf008.plugins.ecommerce.view.ProductView;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;

public class MyPlugin implements IPlugin
{
    public boolean init() {
        IUIController uiController = ICore.getInstance().getUIController();

        ProductRepository productRepository = new ProductRepositoryImp();
        CustomerRepository customerRepository = new CustomerRepositoryImp();
        CartRepository cartRepository = new CartRepositoryImp(productRepository);
        OrderRepository orderRepository = new OrderRepositoryImp(productRepository);

        ProductController productController = new ProductController(new ProductService(productRepository));
        CustomerController customerController = new CustomerController(new CustomerService(customerRepository));
        CartController cartController = new CartController(new CartService(cartRepository, productRepository));
        OrderController orderController = new OrderController(
                new OrderService(orderRepository, cartRepository, productRepository));

        ProductView productView = new ProductView(productController);
        CustomerView customerView = new CustomerView(customerController);
        OrderView orderView = new OrderView(orderController);
        CheckoutView checkoutView = new CheckoutView(productController, customerController, cartController,
                orderController, () -> {
                    productView.refresh();
                    orderView.refresh();
                });

        uiController.createTab("Checkout", checkoutView.getView());
        uiController.createTab("Products", productView.getView());
        uiController.createTab("Customers", customerView.getView());
        uiController.createTab("Orders", orderView.getView());

        MenuItem refreshItem = uiController.createMenuItem("E-commerce", "Refresh data");
        refreshItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                productView.refresh();
                customerView.refresh();
                orderView.refresh();
                checkoutView.refreshCustomersAndProducts();
            }
        });

        return true;
    }
}
