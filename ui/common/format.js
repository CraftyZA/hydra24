// ============================================================================
// FORMAT
// How values read on screen.
//   Format.money(350)                       "R350.00"
//   Format.date('2027-03-10')               "10 Mar 2027"
//   Format.dateRange('2027-03-10', 2)       "10 Mar 2027 (2 days)"
//   Format.dateTime('2027-03-10T18:00:00')  "10 Mar 2027, 18:00"
// ============================================================================

const Format = (function () {
    const LOCALE = 'en-ZA';
    const DATE_OPTIONS = {day: 'numeric', month: 'short', year: 'numeric'};
    const TIME_OPTIONS = {hour: '2-digit', minute: '2-digit', hour12: false};

    function money(amount) {
        return `R${Number(amount).toFixed(2)}`;
    }

    // Dates arrive without a zone. Noon keeps the day right wherever the phone is.
    function date(isoDate) {
        return new Date(`${isoDate}T12:00:00`).toLocaleDateString(LOCALE, DATE_OPTIONS);
    }

    function dateRange(isoDate, days) {
        return days > 1 ? `${date(isoDate)} (${days} days)` : date(isoDate);
    }

    function dateTime(isoDateTime) {
        const value = new Date(isoDateTime);
        return `${value.toLocaleDateString(LOCALE, DATE_OPTIONS)}, ${value.toLocaleTimeString(LOCALE, TIME_OPTIONS)}`;
    }

    return {money: money, date: date, dateRange: dateRange, dateTime: dateTime};
})();
