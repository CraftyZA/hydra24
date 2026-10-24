// ============================================================================
// ICON
// createIcon(pathData) returns a 24x24 decorative SVG icon drawn in the
// current text colour. Load it in <head>, without defer, before any script
// that uses it.
// ============================================================================

function createIcon(pathData) {
    const SVG_NS = 'http://www.w3.org/2000/svg';

    const svg = document.createElementNS(SVG_NS, 'svg');
    svg.setAttribute('viewBox', '0 0 24 24');
    svg.setAttribute('width', '24');
    svg.setAttribute('height', '24');
    svg.setAttribute('aria-hidden', 'true');

    const path = document.createElementNS(SVG_NS, 'path');
    path.setAttribute('d', pathData);
    path.setAttribute('fill', 'currentColor');
    svg.appendChild(path);
    return svg;
}
