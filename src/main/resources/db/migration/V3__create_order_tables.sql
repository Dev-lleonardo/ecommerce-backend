CREATE TABLE orders (

    id         BIGINT AUTO_INCREMENT PRIMARY KEY,

    user_id    BIGINT         NOT NULL,

    status     VARCHAR(20)    NOT NULL DEFAULT 'PENDING',

    created_at DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    total      DECIMAL(38,2)  NOT NULL,

    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(id)

);

CREATE TABLE order_item (

    id         BIGINT        AUTO_INCREMENT PRIMARY KEY,

    order_id   BIGINT        NOT NULL,

    product_id BIGINT        NOT NULL,

    quantity   INT           NOT NULL,

    unit_price DECIMAL(38,2) NOT NULL,

    CONSTRAINT fk_order_item_order   FOREIGN KEY (order_id)   REFERENCES orders(id) ON DELETE CASCADE,

    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES product(id)

);