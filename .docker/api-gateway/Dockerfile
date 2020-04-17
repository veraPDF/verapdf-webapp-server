FROM nginx
LABEL name="api-gateway"
ARG profile

# Remove default nginx configuration
RUN rm /etc/nginx/conf.d/default.conf

# Copy our config file
COPY ./conf.d/${profile}.conf /etc/nginx/conf.d/default.conf
