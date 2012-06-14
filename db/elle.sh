#!/bin/bash

. db_config

dropdb -h $elle_server -p $elle_port -U $elle_pguser $elle_dbname;
createdb -h $elle_server -p $elle_port -U $elle_pguser -O $elle_pguser \
    -T $elle_template $elle_dbname;

# Grant permissions
# -----------------

psql -h $elle_server -p $elle_port -U $elle_pguser \
    $elle_dbname -c "GRANT ALL PRIVILEGES ON DATABASE $elle_dbname TO $elle_pguser;"
