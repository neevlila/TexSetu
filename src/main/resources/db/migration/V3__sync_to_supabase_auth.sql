-- ==============================================================================
-- TEXSETU - B2B TEXTILE MARKETPLACE
-- V3__sync_to_supabase_auth.sql - Automatic Mirror to Supabase auth.users
-- Allows registered users to be visible in Supabase Authentication Dashboard
-- ==============================================================================

CREATE OR REPLACE FUNCTION public.sync_texsetu_user_to_auth()
RETURNS TRIGGER AS $$
BEGIN
    BEGIN
        IF (TG_OP = 'INSERT') THEN
            INSERT INTO auth.users (
                instance_id, id, aud, role, email, encrypted_password, email_confirmed_at, 
                raw_app_meta_data, raw_user_meta_data, created_at, updated_at, is_sso_user, is_anonymous
            ) VALUES (
                '00000000-0000-0000-0000-000000000000'::uuid,
                gen_random_uuid(),
                'authenticated',
                'authenticated',
                NEW.email,
                NEW.password_hash,
                NOW(),
                '{"provider":"email","providers":["email"]}'::jsonb,
                jsonb_build_object('full_name', NEW.full_name, 'role', NEW.role, 'texsetu_id', NEW.id),
                NOW(),
                NOW(),
                false,
                false
            )
            ON CONFLICT (email) WHERE (is_sso_user = false) DO UPDATE SET
                encrypted_password = EXCLUDED.encrypted_password,
                raw_user_meta_data = EXCLUDED.raw_user_meta_data,
                updated_at = NOW();
        ELSIF (TG_OP = 'UPDATE') THEN
            UPDATE auth.users
            SET 
                encrypted_password = NEW.password_hash,
                raw_user_meta_data = jsonb_build_object('full_name', NEW.full_name, 'role', NEW.role, 'texsetu_id', NEW.id),
                updated_at = NOW()
            WHERE email = NEW.email;
        ELSIF (TG_OP = 'DELETE') THEN
            DELETE FROM auth.users WHERE email = OLD.email;
        END IF;
    EXCEPTION WHEN OTHERS THEN
        -- Defensive pass: Never block application registrations if Supabase Auth schema changes
        RAISE NOTICE 'Notice: auth.users sync skipped: %', SQLERRM;
    END;
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS trigger_sync_user_to_auth ON public.users;
CREATE TRIGGER trigger_sync_user_to_auth
AFTER INSERT OR UPDATE OR DELETE ON public.users
FOR EACH ROW
EXECUTE FUNCTION public.sync_texsetu_user_to_auth();
