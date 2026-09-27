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
-- Changes:
--   * Every call inserts a NEW order (never reuses one).
--   * The logged-in waiter's name is resolved server-side and stored on the order.

declare
v_order_id uuid;
begin
    if auth.uid() is null then
        raise exception 'Not authenticated';
end if;

-- Always create a NEW order (one per confirmation), with totals set up front.
insert into public.orders (
    table_id, status, waiter_id, outlet_id, order_number,
    subtotal, tax_amount, total_amount, confirmed_at
)
values (
           p_table_id, 'preparing', p_created_by, p_outlet_id,
           get_next_order_number(p_outlet_id),
           p_subtotal, p_tax, p_total, now()
       )
    returning id into v_order_id;

-- Insert this order's line items.
insert into public.order_items (order_id, menu_item_id, unit_price, total_price, quantity)
select v_order_id,
       nullif(it->>'menu_item_id', '')::uuid,
    (it->>'unit_price')::numeric,
    (it->>'total_price')::numeric,
    (it->>'quantity')::int
from jsonb_array_elements(p_items) as it;

-- Keep the table occupied. (It is freed by the desktop app, not here.)
update public.tables
set status = 'occupied'
where id = p_table_id;

return v_order_id;
end;

$$;