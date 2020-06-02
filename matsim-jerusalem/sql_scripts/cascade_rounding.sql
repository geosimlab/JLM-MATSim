drop type if exists cr_type cascade;
create type cr_type as (int_value int, fp_total float, int_total int);

create or replace function cr_state(state cr_type, fp_value float)
returns cr_type language plpgsql as $$
begin
    state.int_value := round(fp_value + state.fp_total) - state.int_total;
    state.fp_total := state.fp_total + fp_value;
    state.int_total := state.int_total + state.int_value;
    return state;
end $$;

create or replace function cr_final(state cr_type)
returns int language plpgsql as $$
declare
begin
    return state.int_value;
end $$;

create aggregate cascade_rounding_window(float) (
    sfunc = cr_state,
    stype = cr_type,
    finalfunc = cr_final,
    initcond = '(0, 0, 0)'
);