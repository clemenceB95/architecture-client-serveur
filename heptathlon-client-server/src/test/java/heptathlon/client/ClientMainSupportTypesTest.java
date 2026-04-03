package heptathlon.client;

import heptathlon.common.model.Product;
import org.junit.jupiter.api.Test;

import javax.swing.Icon;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientMainSupportTypesTest {

    @Test
    void productOptionToStringIncludesReferencePriceAndStock() throws Exception {
        Product product = new Product("BALLON-001", "football", 25.99, 8);
        Object productOption = newInnerInstance("heptathlon.client.ClientMain$ClientFrame$ProductOption",
                new Class<?>[]{Product.class, int.class},
                product,
                5);

        assertTrue(productOption.toString().contains("BALLON-001"));
        assertTrue(productOption.toString().contains("25"));
        assertTrue(productOption.toString().contains("stock: 5"));
    }

    @Test
    void cartItemAccessorsReturnStoredValues() throws Exception {
        Product product = new Product("VELO-001", "cardio", 499.99, 3);
        Object cartItem = newInnerInstance("heptathlon.client.ClientMain$ClientFrame$CartItem",
                new Class<?>[]{Product.class, int.class},
                product,
                2);

        Method productAccessor = cartItem.getClass().getDeclaredMethod("product");
        Method quantityAccessor = cartItem.getClass().getDeclaredMethod("quantity");
        productAccessor.setAccessible(true);
        quantityAccessor.setAccessible(true);

        assertEquals(product, productAccessor.invoke(cartItem));
        assertEquals(2, quantityAccessor.invoke(cartItem));
    }

    @Test
    void trashIconPaintsWithoutError() throws Exception {
        Icon icon = (Icon) newInnerInstance("heptathlon.client.ClientMain$ClientFrame$TrashIcon",
                new Class<?>[]{int.class, int.class, Color.class},
                16,
                16,
                Color.WHITE);
        BufferedImage image = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            icon.paintIcon(new JPanel(), graphics, 4, 4);
        } finally {
            graphics.dispose();
        }

        assertEquals(16, icon.getIconWidth());
        assertEquals(16, icon.getIconHeight());
    }

    @Test
    void gradientPanelPaintsWithoutError() throws Exception {
        JPanel panel = (JPanel) newInnerInstance("heptathlon.client.ClientMain$ClientFrame$GradientPanel",
                new Class<?>[0]);
        panel.setSize(200, 100);
        BufferedImage image = new BufferedImage(200, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            panel.paint(graphics);
        } finally {
            graphics.dispose();
        }

        assertEquals(200, panel.getWidth());
        assertEquals(100, panel.getHeight());
    }

    private static Object newInnerInstance(String className, Class<?>[] parameterTypes, Object... args) throws Exception {
        Class<?> type = Class.forName(className);
        Constructor<?> constructor = type.getDeclaredConstructor(parameterTypes);
        constructor.setAccessible(true);
        return constructor.newInstance(args);
    }
}
