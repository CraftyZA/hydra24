// ============================================================================
// CAROUSEL CONFIGURATION (Sponsor/promotional images per country)
// ============================================================================

const CAROUSEL_BASE_PATH = '/images/carousel';

// Default carousel settings
const DEFAULT_CAROUSEL = {
    duration: 3000, // ms per image
    items: [
        {image: `${CAROUSEL_BASE_PATH}/2026_Hydra_24_Sponsor_BFontein.png`, alt: 'Sponsor 1', link: null},
        {image: `${CAROUSEL_BASE_PATH}/2026_Hydra_24_Sponsor_PFitness.png`, alt: 'Sponsor 2', link: null},
        {image: `${CAROUSEL_BASE_PATH}/2026_Hydra_24_Sponsor_PrimeView.png`, alt: 'Sponsor 3', link: null}
    ]
};

// ============================================================================
// SOCIAL MEDIA LINKS (shown by common/social.js)
// ============================================================================

const SOCIAL_LINKS = {
    facebook: 'https://www.facebook.com/',
    instagram: 'https://www.instagram.com/',
    tiktok: 'https://www.tiktok.com/'
};
