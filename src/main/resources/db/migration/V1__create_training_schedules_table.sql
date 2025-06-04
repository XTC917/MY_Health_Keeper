CREATE TABLE training_schedules (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    date DATE NOT NULL,
    start_time TIME,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATE NOT NULL,
    updated_at DATE NOT NULL,
    
    CONSTRAINT fk_training_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_training_course FOREIGN KEY (course_id) REFERENCES courses(id)
); 