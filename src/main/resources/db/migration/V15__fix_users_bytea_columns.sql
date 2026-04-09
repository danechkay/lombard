DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'users'
          AND column_name = 'email'
          AND data_type = 'bytea'
    ) THEN
        ALTER TABLE users
            ALTER COLUMN email TYPE VARCHAR(255)
            USING convert_from(email, 'UTF8');
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'users'
          AND column_name = 'password_hash'
          AND data_type = 'bytea'
    ) THEN
        ALTER TABLE users
            ALTER COLUMN password_hash TYPE VARCHAR(255)
            USING convert_from(password_hash, 'UTF8');
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'users'
          AND column_name = 'full_name'
          AND data_type = 'bytea'
    ) THEN
        ALTER TABLE users
            ALTER COLUMN full_name TYPE VARCHAR(255)
            USING convert_from(full_name, 'UTF8');
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'users'
          AND column_name = 'phone'
          AND data_type = 'bytea'
    ) THEN
        ALTER TABLE users
            ALTER COLUMN phone TYPE VARCHAR(20)
            USING CASE WHEN phone IS NULL THEN NULL ELSE convert_from(phone, 'UTF8') END;
    END IF;
END $$;

