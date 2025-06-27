$(document).ready(function () {
    const list = document.getElementById('toc-integration-services-0')
    Array.from(list.children)
        .sort((a, b) => a.textContent.trim().toLowerCase().localeCompare(b.textContent.trim().toLowerCase()))
        .forEach((item) => list.appendChild(item))
});