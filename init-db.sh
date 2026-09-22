#!/bin/bash
set -e

echo "Waiting for SQL Server to be ready..."
until /opt/mssql-tools18/bin/sqlcmd -S sqlserver -U sa -P "$MSSQL_SA_PASSWORD" -C -Q "SELECT 1" &>/dev/null; do
  echo "Not ready yet, retrying in 3s..."
  sleep 3
done

echo "SQL Server is up. Creating database if not exists..."
/opt/mssql-tools18/bin/sqlcmd -S sqlserver -U sa -P "$MSSQL_SA_PASSWORD" -C -Q "IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'bekal') CREATE DATABASE bekal;"
echo "Database ready."