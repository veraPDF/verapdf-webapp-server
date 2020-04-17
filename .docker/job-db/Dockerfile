FROM postgres:11.7
COPY create-job-db.sql /docker-entrypoint-initdb.d/create-job-db.sql
ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["postgres"]
