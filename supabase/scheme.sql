-- ---------- Atomic transfer ----------

create or replace function public.transfer_order(p_order_id uuid, p_to_table uuid)
returns void language plpgsql security definer as $$
declare v_from uuid;
begin
select table_id into v_from from public.orders where id = p_order_id;
update public.orders set table_id = p_to_table where id = p_order_id;
update public.tables set status = 'occupied' where id = p_to_table;
-- free the old table only if it has no other active orders
update public.tables t set status = 'free'
where t.id = v_from
  and not exists (
    select 1 from public.orders o
    where o.table_id = v_from and o.status in ('open', 'confirmed')
);
end; $$;

-- ---------- Atomic order placement ----------
-- Creates/appends the order, inserts items, confirms it, and occupies the table
-- in ONE transaction. Any failure rolls the whole thing back.
create or replace function public.place_order(
    p_table_id uuid,
    p_outlet_id uuid,
    p_items jsonb,
    p_subtotal numeric,
    p_tax numeric,
    p_total numeric,
    p_created_by uuid default null
) returns uuid
language plpgsql
security definer
as $$
declare
v_order_id uuid;
begin
    if auth.uid() is null then
        raise exception 'Not authenticated';
end if;

    -- Reuse the table's existing open/confirmed order, else create one.
select id into v_order_id
from public.orders
where table_id = p_table_id and status in ('open', 'confirmed')
    limit 1;

if v_order_id is null then
        insert into public.orders (table_id, status, waiter_id, outlet_id, order_number)
        values (p_table_id, 'confirmed', p_created_by, p_outlet_id, get_next_order_number(p_outlet_id))
        returning id into v_order_id;
end if;

    -- Insert the new line items from the jsonb array.
insert into public.order_items (order_id, menu_item_id, name, unit_price, total_price, quantity)
select v_order_id,
       nullif(it->>'menu_item_id', '')::uuid,
    it->>'name',
    (it->>'unit_price')::numeric,
    (it->>'total_price')::numeric,
    (it->>'quantity')::int
from jsonb_array_elements(p_items) as it;

-- Confirm the order and occupy the table.
update public.orders
set status = 'confirmed',
    subtotal = p_subtotal,
    tax = p_tax,
    total = p_total,
    confirmed_at = now()
where id = v_order_id;

update public.tables
set status = 'occupied'
where id = p_table_id;

return v_order_id;
end; $$;