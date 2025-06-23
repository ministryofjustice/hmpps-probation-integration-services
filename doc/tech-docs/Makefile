.PHONY: package preview
.DEFAULT_GOAL := preview

package:
	docker compose run --rm publisher /usr/local/bin/package

preview:
	docker compose run --rm --service-ports publisher /usr/local/bin/preview

link-check:
	lychee --verbose --no-progress './**/*.md' './**/*.html' './**/*.erb' --accept 401,403,200,429
