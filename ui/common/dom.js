// ============================================================================
// DOM
// Builds elements with text set safely, so nothing untrusted is parsed as HTML.
//   el('a', {class: 'x', href: '/y', text: 'Go'})    props and text
//   el('div', {class: 'x'}, child, 'text', ...)      and/or children
//   showStatus(element, message, isError)            message line; '' hides it
// ============================================================================

function el(tag, props, ...children) {
    const element = document.createElement(tag);
    Object.entries(props || {}).forEach(function ([name, value]) {
        if (name === 'text') {
            element.textContent = value;
        } else if (name === 'class') {
            element.className = value;
        } else {
            element[name] = value;
        }
    });
    element.append(...children);
    return element;
}

function showStatus(element, message, isError) {
    element.textContent = message;
    element.hidden = !message;
    element.classList.toggle('is-error', Boolean(isError));
}
