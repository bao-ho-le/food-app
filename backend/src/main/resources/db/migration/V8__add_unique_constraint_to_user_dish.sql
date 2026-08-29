ALTER TABLE user_dish
    ADD CONSTRAINT uq_user_dish_user_dish UNIQUE (user_id, dish_id);