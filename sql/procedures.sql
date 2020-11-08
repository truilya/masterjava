create or replace procedure prc_fill_user_groups()
    language sql
as
$$
insert into user_group(user_id, group_id)
select u.id, tug.group_id
from users u
         join tmp_user_group tug on tug.email = u.email;
delete
from tmp_user_group;
$$;