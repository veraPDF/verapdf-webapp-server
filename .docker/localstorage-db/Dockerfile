FROM postgres:11
COPY ./createdb.sql /docker-entrypoint-initdb.d/createdb.sql
ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["postgres"]
