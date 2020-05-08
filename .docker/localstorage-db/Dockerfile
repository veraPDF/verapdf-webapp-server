FROM postgres:11.7
COPY ./create-file-storage-db.sql /docker-entrypoint-initdb.d/create-file-storage-db.sql
ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["postgres"]
