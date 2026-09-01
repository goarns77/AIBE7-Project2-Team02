-- Apply once to an existing PostgreSQL database before deploying SELLER role code.
alter table users drop constraint if exists ck_users_role;
alter table users
    add constraint ck_users_role check (role in ('USER', 'SELLER', 'ADMIN'));

-- Backfill accounts approved before seller role promotion was introduced.
update users
set role = 'SELLER',
    token_version = token_version + 1,
    updated_at = current_timestamp
where role = 'USER'
  and exists (
      select 1
      from seller_profiles
      where seller_profiles.user_id = users.user_id
        and seller_profiles.verification_status = 'APPROVED'
  );
