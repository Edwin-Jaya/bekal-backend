#!/bin/bash

echo "Menunggu SQL Server siap..."
for i in {1..50}; do
    /opt/mssql-tools18/bin/sqlcmd -S sqlserver -U sa -P "$MSSQL_SA_PASSWORD" -C -Q "SELECT 1" > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo "SQL Server berhasil berjalan!"
        break
    fi
    if [ $i -eq 50 ]; then
        echo "ERROR: SQL Server tidak merespons setelah 50 percobaan!"
        exit 1
    fi
    echo "SQL Server belum siap, menunggu 2 detik... ($i/50)"
    sleep 2
done

echo "Mengkonversi script.sql ke UTF-8..."
iconv -f UTF-16LE -t UTF-8 /script.sql -o /tmp/script_utf8.sql
if [ $? -ne 0 ]; then
    echo "ERROR: Gagal mengkonversi script.sql!"
    exit 1
fi

echo "Menjalankan script.sql..."
/opt/mssql-tools18/bin/sqlcmd -S sqlserver -U sa -P "$MSSQL_SA_PASSWORD" -C -i /tmp/script_utf8.sql
if [ $? -ne 0 ]; then
    echo "ERROR: Gagal menjalankan script.sql!"
    exit 1
fi

echo "Inisialisasi database selesai!"